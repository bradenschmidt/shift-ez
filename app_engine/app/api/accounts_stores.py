import datetime
import uuid

from flask import jsonify, Blueprint, request

from app.api.accounts_schedules import sort_schedules
from app.models.account import Account
from app.models.sharedStoreDepartment import SharedStoreDepartment
from app.models.storeDepartment import StoreDepartment
from app.utils.errors import Errors

accounts_stores = Blueprint("accounts_stores", __name__)


# POSTS ##########################
@accounts_stores.route('/add', methods=['POST'])
def add_store_to_account(user_id):
    """Create a new store and add to an account."""
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    account = Account.get(user_id)
    if not account:
        # Setup results
        code = Errors.account_not_found
        desc = 'Add Store to Account Failed. Account does not exist.'

        return jsonify(code=code, desc=desc)

    store = StoreDepartment.get(user_id, store_name, dep_name)
    if not store:
        store = StoreDepartment(user_id=user_id,
                                store_name=store_name,
                                dep_name=dep_name,
                                schedules=[])
        store.put()

    exists = account.is_store_in_account(store)
    if not exists:
        account.store_deps.append(store.key)
        account.put()

        # Setup results
        code = 0
        desc = 'Store Added to Account Successfully.'
    else:
        # Setup results
        code = Errors.store_in_account
        desc = 'Store Already Exists in Account.'

    return jsonify(code=code, desc=desc)


@accounts_stores.route('/share', methods=['POST'])
def share_store(user_id):
    """Share Store by adding store to SharedStoreDepartment with key.
    """
    store_user_id = request.args.get('store_user_id')
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    # make a random UUID
    u = uuid.uuid4()
    key = u.hex

    account = Account.get(user_id)

    if account:
        store = account.get_store_from_account(store_user_id,
                                               store_name,
                                               dep_name)
        if store:
            store_dep_key = store.key
            shared_store = SharedStoreDepartment(share_key=key,
                                                 store_dep_key=store_dep_key)
            shared_store.put()

            # Setup results
            code = 0
            desc = 'Share Successful. Key valid for 7 days.'
        else:
            # Setup results
            code = 1
            desc = 'Share Unsuccessful. Store Not Found.'
            key = None
    else:
        # Setup results
        code = 2
        desc = 'Share Unsuccessful. Account Not Found.'
        key = None

    return jsonify(code=code, desc=desc, key=key)


@accounts_stores.route('/join', methods=['POST'])
def join_store(user_id):
    """Join Store by adding store to users account found in
    SharedStoreDepartment by provided key.
    """
    key = request.args.get('key')

    account = Account.get(user_id)
    if not account:
        # Setup account not found error
        code = Errors.account_not_found
        desc = 'Join Failed: Account Not Found.'
        return jsonify(code=code, desc=desc)

    shared_store = SharedStoreDepartment.get(key)
    # Check if a store was returned
    if not shared_store:
        # Setup store key not found error
        code = Errors.key_not_found
        desc = 'Join Failed: Key Not Found.'
        return jsonify(code=code, desc=desc)

    date = shared_store.shared_dateTime

    if date < (datetime.datetime.now() - datetime.timedelta(days=7)):
        # Expired
        code = Errors.key_too_old
        desc = 'Join Failed: Key Too Old (Over 7 Days).'
        return jsonify(code=code, desc=desc)

    store = shared_store.get(key)

    if not store:
        # Expired
        code = Errors.store_not_found
        desc = 'Join Failed: Store Not Found.'
        return jsonify(code=code, desc=desc)

    account.store_deps.append(store.store_dep_key)
    account.put()

    # Setup results
    code = 0
    desc = 'Join Successful'
    return jsonify(code=code, desc=desc)


# GETS #############################################################
@accounts_stores.route('/all')
def get_accounts_stores(user_id):
    """Return all of the stores for the given account"""

    account = Account.get(user_id)

    if account:
        account_dict = account.to_dict_stores()
        return jsonify(stores=account_dict['store_deps'])
    else:
        return jsonify(stores=None)


@accounts_stores.route('/<store_name>/dep/<dep_name>/year/<year>')
def get_schedules_by_year_store(user_id, store_name, dep_name, year):
    """Return the users schedules for a given year and store."""
    store_user_id = request.args.get('store_user_id')
    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    year_schedules = []
    if account and store:
        if account.is_store_in_account(store):
            schedules = store.get_schedules()
            for schedule in schedules:
                if schedule.year == int(year):
                    year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sort_schedules(year_schedules, reverse))


@accounts_stores.route('/<store_name>/dep/<dep_name>/year/<year>/week/<week>/')
def get_schedules_by_year_store_dep(user_id, store_name, dep_name, year, week):
    """Return the users schedules for a given year and week and store"""
    store_user_id = request.args.get('store_user_id')
    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    year_schedules = []
    if account and store:
        if account.is_store_in_account(store):
            schedules = store.get_schedules()
            for schedule in schedules:
                if schedule.year == int(year) and schedule.week == week:
                    year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sort_schedules(year_schedules, reverse))


@accounts_stores.route('/<store_name>/dep/<dep_name>/')
def get_schedules_by_store(user_id, store_name, dep_name, store_user_id=None):
    """Return the users schedules for a store"""

    if not store_user_id:
        store_user_id = request.args.get('store_user_id')

    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    schedules = []
    if account and store:
        if account.is_store_in_account(store):
            schedules = store.get_schedule_dicts()

    return jsonify(schedules=sort_schedules(schedules, reverse))


# DELETES ###########################################################
@accounts_stores.route('/remove', methods=['DELETE'])
def remove_store_from_account(user_id):
    """Remove a store from an account."""
    store_user_id = request.args.get('store_user_id')
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    account = Account.get(user_id)
    if not account:
        # Setup results
        code = Errors.account_not_found
        desc = 'Remove Store From Account Failed. Account does not exist.'

        return jsonify(code=code, desc=desc)

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    if not store:
        # Setup store not found error
        code = Errors.store_not_found
        desc = 'Remove Store Failed: Store Not Found.'
        return jsonify(code=code, desc=desc)

    exists = account.is_store_in_account(store)
    if exists:
        account.store_deps.remove(store.key)

        account.put()

        # Setup results
        code = 0
        desc = 'Store Removed from Account Successfully.'
    else:
        # Setup results
        code = Errors.store_in_account
        desc = 'Store Already Exists in Account.'

    return jsonify(code=code, desc=desc)
