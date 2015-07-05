import os
import logging

import jinja2

from flask import Flask, jsonify, request

from operator import itemgetter

from google.appengine.api import images  # , users
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


def jsonifySchedules(schedules, reverse):
    # convert to dicts
    schedules = [s.to_dict() for s in schedules]

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

    # Convert blob key to image url for each schedule, the remove blob key
    for s in schedules:
        s['image'] = images.get_serving_url(s['image_blob'])
        del s['image_blob']

    return jsonify(schedules=schedules)


def listifySchedules(schedules, reverse):
    # convert to dicts
    schedules = [s.to_dict() for s in schedules]

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

    # Convert blob key to image url for each schedule, the remove blob key
    for s in schedules:
        s['image'] = images.get_serving_url(s['image_blob'])
        del s['image_blob']

    return schedules


def getAllSchedules():
    schedules = Schedule.query().fetch()
    return listifySchedules(schedules, None)


def prepAccountForJsonify(accountModel):
    account = accountModel.to_dict()
    stores = []
    for store_key in account['stores']:
        storeObj = store_key.get()
        if storeObj:
            store = storeObj.to_dict()
            stores.append(store)
    account['stores'] = stores

    return account


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
    upload_url = blobstore.create_upload_url('/api/upload_image')

    template_values = {
        'upload_url': upload_url
    }

    template = JINJA_ENVIRONMENT.get_template('upload.html')
    return template.render(template_values)


# API ENDPOINTS  #############################################################
# POSTS  #####################################################################

@app.route('/api/upload_image', methods=['POST'])
def uploadImage():
    """ Accept an schedule with info and image"""
    image = request.files['file']

    header = image.headers['Content-Type']
    parsed_header = parse_options_header(header)
    blob_key_str = parsed_header[1]['blob-key']

    logging.info(blob_key_str)

    user_id = request.form.get('user_id')
    year = request.form.get('year', type=int)
    week = request.form.get('week', type=int)
    week_offset = request.form.get('week_offset', type=int)

    schedule = Schedule(user_id=user_id,
                        year=year,
                        week=week,
                        week_offset=week_offset,
                        image_blob=blobstore.BlobKey(blob_key_str))

    schedule.put()

    # Setup results
    code = 0
    desc = 'Upload Successful'

    return jsonify(code=code, desc=desc)


@app.route('/api/stores/add', methods=['POST'])
def addStore():
    """Add a new store."""
    user_id = request.args.get('user_id')
    store_name = request.args.get('store_name')

    store = Store.query(ndb.AND(Store.store_name == store_name,
                                Store.user_id == user_id)).get()

    if(store):
        # Setup results
        code = 1
        desc = 'Store Already Exists.'

        return jsonify(code=code, desc=desc)
    else:
        store = Store(user_id=user_id, store_name=store_name, deps=[])

        store.put()

        # Setup results
        code = 0
        desc = 'Store Added Successful'

    return jsonify(code=code, desc=desc)


def addStore2():
    """Add a new store."""
    store_name = request.args.get('store')
    depsJson = request.args.get('deps')

    deps = json.loads(depsJson)

    store = Store.query(Store.store_name == store_name).get()

    if(store):
        print deps
        for dep in deps:
            if dep not in store.deps:
                store.deps.append(dep)
    else:
        store = Store(store_name=store_name, deps=deps)

    store.put()

    # Setup results
    code = 0
    desc = 'Add Store Successful'

    return jsonify(code=code, desc=desc)


@app.route('/api/accounts/<user_id>/stores/add', methods=['POST'])
def addStoreToAccount(user_id):
    """Add a store to an account. Create new store if required."""
    store_name = request.args.get('store_name')

    account = Account.query(Account.user_id == user_id).get()

    if(not account):
        # Setup results
        code = 1
        desc = 'Account does not exist.'

        return jsonify(code=code, desc=desc)

    exists = False
    for store_key in account.stores:
        print store_key
        store = store_key.get()
        if store:
            if store.store_name == store_name:
                exists = True

    if(not exists):
        store = Store(user_id=user_id, store_name=store_name, deps=[])
        new_store_key = store.put()

        account.stores.append(new_store_key)
        account.put()

        # Setup results
        code = 0
        desc = 'Store Added to Account Successfully'
    else:
        # Setup results
        code = 0
        desc = 'Store Already Exists in Account.'

    return jsonify(code=code, desc=desc)


@app.route('/api/accounts/add', methods=['POST'])
def addAccount():
    """Add a new Account"""
    user_id = request.args.get('user_id')
    user_name = request.args.get('user_name')

    accounts = Account.query(Account.user_id == user_id).fetch(1)

    if(accounts):
        # Setup results
        code = 1
        desc = 'Account Already Exists'
        account = prepAccountForJsonify(accounts[0])
    else:
        accountModel = Account(user_id=user_id,
                               user_name=user_name,
                               stores=[])
        accountModel.put()
        account = prepAccountForJsonify(accountModel)

        # Setup results
        code = 0
        desc = 'Account Added Successfully'

    return jsonify(code=code, desc=desc, account=account)


@app.route('/api/accounts/<user_id>/stores/share', methods=['POST'])
def shareStore(user_id):
    store_name = request.args.get('store_name')
    dep_name = request.args.get('dep_name')

    # make a random UUID
    u = uuid.uuid4()
    key = u.hex

    account = Account.query(Account.user_id == user_id).get()

    # if(account):
    #     for store_key in account.stores:
    #         if store_key.get().store_name == store_name:
                # for dep_key

    # sharedDepartment = SharedDepartment(key=key, dep_key=dep_key)

    # sharedStore.put()

    # Setup results
    code = 0
    desc = 'Share Successful. Key valid for 7 days.'
    return jsonify(code=code, desc=desc, key=key)


@app.route('/api/stores/join', methods=['POST'])
def joinStore():
    key = request.args.get('key')
    user_id = request.args.get('user_id')

    shared_stores = SharedStore.query(SharedStore.key == key).fetch(1)

    # Check if a store was returned
    if len(shared_stores) <= 0:
        # Setup store key not found error
        code = 1
        desc = 'Join Failed: Key Not Found.'
        return jsonify(code=code, desc=desc)

    shared_store = shared_stores[0]

    print shared_store

    date = shared_store.upload_dateTime

    if (date < (datetime.datetime.now()-datetime.timedelta(days=7))):
        # Expired
        code = 2
        desc = 'Join Failed: Key Too Old (Over 7 Days).'
        return jsonify(code=code, desc=desc)

    new_store = Store(user_id=user_id,
                      store=shared_store.store_name,
                      deps=shared_store.dep)

    new_store.put()

    # Setup results
    code = 0
    desc = 'Join Successful'
    return jsonify(code=code, desc=desc)


# GETS  ######################################################################
@app.route('/api/accounts/<user_id>')
def getAccount(user_id):
    """Return the Account Info"""

    account = Account.query(Account.user_id == user_id).get()

    if(account):
        accountDict = prepAccountForJsonify(account)
    else:
        account = None

    return jsonify(account=accountDict)


@app.route('/api/stores/all')
def getStores():
    """Return all of the stores"""

    stores = Store.query().fetch()

    # convert to dicts
    stores = [s.to_dict() for s in stores]

    return jsonify(stores=stores)


@app.route('/api/accounts/<user_id>/stores/all')
def getAccountsStores(user_id):
    """Return all of the stores for the given account"""

    account = Account.query(Account.user_id == user_id).get()

    if(account):
        stores = []
        for store_key in account.stores:
            store = store_key.get()
            if store:
                stores.append(store.to_dict())
            else:
                logging.info("Store with given key is missing: "
                             + str(store_key.flat()))
        return jsonify(stores=stores)
    else:
        return jsonify(stores=None)


@app.route('/api/upload/link')
def uploadImageLink():
    """Return a blobstore upload link as json for the client to upload an
    image.
    """
    upload_url = blobstore.create_upload_url('/api/upload_image')

    return jsonify(upload_url=upload_url)


@app.route('/api/accounts/<user_id>/schedules/all')
def getSchedules(user_id):
    """Return all of the users schedules"""
    reverse = request.args.get('reverse')

    account = Account.query(Account.user_id == user_id).get()

    for store_key in account.stores:
        store = store_key.get()
        if store:


    schedules = Schedule.query(Schedule.user_id == user_id).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/year/<year>')
def getSchedulesByYear(year):
    """Return the users schedules for a given year"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(ndb.AND(Schedule.user_id == user_id,
                                       Schedule.year == int(year))).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/year/<year>/week/<week>')
def getSchedulesByYearWeek(year, week):
    """Return the users schedules for a given year and week"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(ndb.AND(Schedule.user_id == user_id,
                                       Schedule.year == int(year),
                                       Schedule.week == int(week))).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/year/<year>/store/<store>')
def getSchedulesByYearStore(year, store):
    """Return the users schedules for a given year and store"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(ndb.AND(Schedule.user_id == user_id,
                                       Schedule.year == int(year),
                                       Schedule.store == store)).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/year/<year>/store/<store>/dep/<dep>')
def getSchedulesByYearStoreDep(year, store, dep):
    """Return the users schedules for a given year and Store and Dep"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(ndb.AND(Schedule.user_id == user_id,
                               Schedule.year == int(year),
                               Schedule.store == store),
                               Schedule.dep == dep).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/store/<store>')
def getSchedulesByStore(store):
    """Return the users schedules for a store"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(ndb.AND(Schedule.user_id == user_id,
                                       Schedule.store == store)).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/schedules/store/<store>/dep/<dep>')
def getSchedulesByStoreDep(store, dep):
    """Return the users schedules for a given Store and Dep"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

    schedules = Schedule.query(
                    ndb.AND(Schedule.user_id == user_id,
                            Schedule.store == store,
                            Schedule.dep == dep)).fetch()

    return jsonifySchedules(schedules, reverse)


@app.route('/api/help', methods=['GET'])
def help():
    """Print available functions."""
    func_list = {}
    for rule in app.url_map.iter_rules():
        if rule.endpoint != 'static':
            doc = app.view_functions[rule.endpoint].__doc__
            func_list[rule.rule] = re.sub(r"\s+", " ", doc)
    return jsonify(func_list)
