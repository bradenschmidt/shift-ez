import os
import logging

import jinja2

from flask import Flask, jsonify, request

from operator import itemgetter

# from google.appengine.api import users
from google.appengine.ext import ndb, blobstore

from werkzeug.http import parse_options_header

from models.account import Account
from models.storeDepartment import StoreDepartment
from models.sharedStoreDepartment import SharedStoreDepartment
from models.schedule import Schedule

import json  # get list of deps
import re
import uuid  # For key generator

import datetime  # Key Expiry

from utils.errors import Errors


"""
    Use 'dev_appserver.py .' to deploy locally for testing
    Use 'appcfg.py -A shift-ez update app' to deploy on gae
"""

app = Flask(__name__)
app.config['DEBUG'] = True

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

DEFAULT_SCHEDULE_NAME = 'default_schedule'
DEFAULT_STORE_NAME = 'default_store'


def schedule_key(schedule_name=DEFAULT_SCHEDULE_NAME):
    """Constructs a Datastore key for a Schedule entity.
    We use schedule_name as the key.
    """
    return ndb.Key('Schedule', schedule_name)


def store_key(store_name=DEFAULT_STORE_NAME):
    """Constructs a Datastore key for a Schedule entity.
    We use schedule_name as the key.
    """
    return ndb.Key('Store', store_name)


def sortSchedules(schedules, reverse):
    # sort
    if(len(schedules) > 0):
        if(reverse == 'true'):
            # Sort schedules by year then week with newest schedule first
            schedules = sorted(schedules, key=itemgetter('week'), reverse=True)
            schedules = sorted(schedules, key=itemgetter('year'), reverse=True)
        else:
            # Sort schedules by year then week with oldest schedule first
            schedules = sorted(schedules, key=itemgetter('week'))
            schedules = sorted(schedules, key=itemgetter('year'))

    return schedules


def getAllSchedules():
    schedules = Schedule.query().fetch()
    schedules = [s.to_dict_images() for s in schedules]
    return sortSchedules(schedules, None)


# Pages ######################################################################
@app.route('/')
def index():
    """Serve the homepage."""

    # user = users.get_current_user()
    # if user:
    #     url = users.create_logout_url('')
    #     url_linktext = 'Logout'
    # else:
    #     url = users.create_login_url('')
    #     url_linktext = 'Login'

    schedules = getAllSchedules()

    template_values = {
        'schedules': schedules
    }

    template = JINJA_ENVIRONMENT.get_template('index.html')
    return template.render(template_values)


@app.route('/hello')
def hello():
    """Return a friendly HTTP greeting."""
    return 'Hello World!'


@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404


@app.route('/upload/form')
def uploadImageForm():
    """Show an upload form"""
    upload_url = blobstore.create_upload_url('/api/stores/schedules/add')

    template_values = {
        'upload_url': upload_url
    }

    template = JINJA_ENVIRONMENT.get_template('upload.html')
    return template.render(template_values)


# API ENDPOINTS  #############################################################
# POSTS  #####################################################################

@app.route('/api/stores/schedules/add', methods=['POST'])
def uploadImage():
    """ Accept an schedule with info and image"""
    image = request.files['file']

    header = image.headers['Content-Type']
    parsed_header = parse_options_header(header)
    blob_key_str = parsed_header[1]['blob-key']

    store_user_id = request.form.get('store_user_id')
    store_name = request.form.get('store_name')
    dep_name = request.form.get('dep_name')

    user_id = request.form.get('user_id')
    year = request.form.get('year', type=int)
    week = request.form.get('week', type=int)
    week_offset = request.form.get('week_offset', type=int)

    store = StoreDepartment.get(store_user_id, store_name, dep_name)

    if not store:
        # Setup store not found error
        code = Errors.store_not_found
        desc = 'Upload Failed: Store Not Found.'
        return jsonify(code=code, desc=desc)

    account = Account.get(user_id)
    if not account:
        # Setup store not found error
        code = Errors.account_not_found
        desc = 'Upload Failed: Account Not Found.'
        return jsonify(code=code, desc=desc)

    schedule = Schedule(parent=store.key,
                        upload_user_id=user_id,
                        upload_user_name=account.user_name,
                        year=year,
                        week=week,
                        week_offset=week_offset,
                        image_blob=blobstore.BlobKey(blob_key_str))

    schedule.put()

    store.schedules.append(schedule.key)
    store.put()

    # Setup results
    code = 0
    desc = 'Upload Successful'

    return jsonify(code=code, desc=desc)


@app.route('/api/stores/add', methods=['POST'])
def addStore():
    """Add a new store department."""
    user_id = request.args.get('user_id')
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    store = StoreDepartment.get(user_id, store_name, dep_name)

    if(store):
        # Setup results
        code = 1
        desc = 'Store Already Exists.'

        return jsonify(code=code, desc=desc)
    else:
        store = StoreDepartment(user_id=user_id,
                                store_name=store_name,
                                dep_name=dep_name,
                                schedules=[])
        store.put()

        # Setup results
        code = 0
        desc = 'Store Added Successful'

    return jsonify(code=code, desc=desc)


@app.route('/api/accounts/<user_id>/stores/add', methods=['POST'])
def addStoreToAccount(user_id):
    """Add an existing store to an account."""
    store_user_id = request.args.get('store_user_id')
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    account = Account.get(user_id)
    if(not account):
        # Setup results
        code = Errors.account_not_found
        desc = 'Add Store to Account Failed. Account does not exist.'

        return jsonify(code=code, desc=desc)

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    if not store:
        # Setup store not found error
        code = Errors.store_not_found
        desc = 'Add Store to Account Failed. Store not found.'

        return jsonify(code=code, desc=desc)

    exists = account.isStoreInAccount(store)

    if(not exists):
        account.storeDeps.append(store.key)
        account.put()

        # Setup results
        code = 0
        desc = 'Store Added to Account Successfully.'
    else:
        # Setup results
        code = Errors.store_in_account
        desc = 'Store Already Exists in Account.'

    return jsonify(code=code, desc=desc)


@app.route('/api/accounts/add', methods=['POST'])
def addAccount():
    """Add a new Account"""
    user_id = request.args.get('user_id')
    user_name = request.args.get('user_name')

    account = Account.get(user_id)

    if(account):
        # Setup results
        code = 1
        desc = 'Account Already Exists'
        account = account.to_dict_stores()
    else:
        new_account_model = Account(user_id=user_id,
                                    user_name=user_name,
                                    storeDeps=[])
        new_account_model.put()
        account = new_account_model.to_dict_stores()

        # Setup results
        code = 0
        desc = 'Account Added Successfully'

    return jsonify(code=code, desc=desc, account=account)


@app.route('/api/accounts/<user_id>/stores/share', methods=['POST'])
def shareStore(user_id):
    """Share Store by adding store to SharedStoreDepartment with key.
    """
    created_user_id = request.args.get('created_user_id')
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    # make a random UUID
    u = uuid.uuid4()
    key = u.hex

    account = Account.get(user_id)

    if account:
        store = account.getStoreFromAccount(created_user_id,
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


@app.route('/api/accounts/<user_id>/stores/join', methods=['POST'])
def joinStore(user_id):
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

    if (date < (datetime.datetime.now()-datetime.timedelta(days=7))):
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

    account.storeDeps.append(store.store_dep_key)
    account.put()

    # Setup results
    code = 0
    desc = 'Join Successful'
    return jsonify(code=code, desc=desc)


# GETS  ######################################################################
@app.route('/api/accounts/<user_id>')
def getAccount(user_id):
    """Return the Account Info"""

    account = Account.get(user_id)

    if(account):
        accountDict = account.to_dict_stores()
    else:
        accountDict = None

    return jsonify(account=accountDict)


@app.route('/api/stores/all')
def getStores():
    """Return all of the stores"""

    stores = StoreDepartment.query().fetch()

    # convert to dicts
    stores = [s.to_dict() for s in stores]

    return jsonify(stores=stores)


@app.route('/api/accounts/<user_id>/stores/all')
def getAccountsStores(user_id):
    """Return all of the stores for the given account"""

    account = Account.get(user_id)

    if(account):
        accountDict = account.to_dict_stores()
        return jsonify(stores=accountDict['storeDeps'])
    else:
        return jsonify(stores=None)


@app.route('/api/stores/schedules/link')
def uploadImageLink():
    """Return a blobstore upload link as json for the client to upload an
    image.
    """
    upload_url = blobstore.create_upload_url('/api/stores/schedules/add')

    return jsonify(upload_url=upload_url)


@app.route('/api/accounts/<user_id>/schedules/all')
def getSchedules(user_id):
    """Return all of the users schedules sorted in reverse if specified."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    schedules = []
    if account:
        schedules = account.getScheduleDicts()

    return sortSchedules(schedules, reverse)


@app.route('/api/accounts/<user_id>/schedules/year/<year>')
def getSchedulesByYear(user_id, year):
    """Return the users schedules for a given year."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    year_schedules = []
    if account:
        schedules = account.getSchedules()
        for schedule in schedules:
            if schedule.year == int(year):
                year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sortSchedules(year_schedules, reverse))


@app.route('/api/accounts/<user_id>/schedules/year/<year>/week/<week>')
def getSchedulesByYearWeek(user_id, year, week):
    """Return the users schedules for a given year and week."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    week_schedules = []
    if account:
        schedules = account.getSchedules()
        for schedule in schedules:
            if schedule.year == int(year) and schedule.week == int(week):
                week_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sortSchedules(week_schedules, reverse))


@app.route('''/api/accounts/<user_id>/stores/<store_name>\
/dep/<dep_name>/year/<year>''')
def getSchedulesByYearStore(user_id, store_name, dep_name, year):
    """Return the users schedules for a given year and store."""
    store_user_id = request.args.get('store_user_id')
    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    year_schedules = []
    if account and store:
        if account.isStoreInAccount(store):
            schedules = store.getSchedules()
            for schedule in schedules:
                if schedule.year == int(year):
                    year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sortSchedules(year_schedules, reverse))


@app.route('''/api/accounts/<user_id>/stores/<store_name>/dep/<dep_name>/\
year/<year>/week/<week>/''')
def getSchedulesByYearStoreDep(user_id, store_name, dep_name, year, week):
    """Return the users schedules for a given year and week and store"""
    store_user_id = request.args.get('store_user_id')
    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    year_schedules = []
    if account and store:
        if account.isStoreInAccount(store):
            schedules = store.getSchedules()
            for schedule in schedules:
                if schedule.year == int(year) and schedule.week == week:
                    year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sortSchedules(year_schedules, reverse))


@app.route('/api/accounts/<user_id>/stores/<store_name>/dep/<dep_name>/')
def getSchedulesByStore(user_id, store_name, dep_name):
    """Return the users schedules for a store"""

    store_user_id = request.args.get('store_user_id')
    reverse = request.args.get('reverse')

    store = StoreDepartment.get(store_user_id, store_name, dep_name)
    account = Account.get(user_id)
    schedules = []
    if account and store:
        if account.isStoreInAccount(store):
            schedules = store.getScheduleDicts()

    return jsonify(schedules=sortSchedules(schedules, reverse))


@app.route('/api/help', methods=['GET'])
def help():
    """Print available functions."""
    func_list = {}
    for rule in app.url_map.iter_rules():
        if rule.endpoint != 'static':
            doc = app.view_functions[rule.endpoint].__doc__
            func_list[rule.rule] = re.sub(r"\s+", " ", doc)
    return jsonify(func_list)
