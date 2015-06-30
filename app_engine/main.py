import os
import logging

import jinja2

from flask import Flask, jsonify, request

from operator import itemgetter

from google.appengine.api import images  # , users
from google.appengine.ext import ndb, blobstore

from werkzeug.http import parse_options_header

from models.schedule import Schedule
from models.store import Store

import re


"""
    Use 'appcfg.py -A shift-ez update app' to deploy on gae
    Use 'dev_appserver.py .' to deploy locally for testing
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


# Pages ######################################################################
@app.route('/')
def index():
    """Serve the homepage"""

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

    store = request.form.get('store')
    user_id = request.form.get('user_id')
    user_name = request.form.get('user_name')
    dep = request.form.get('dep')
    year = request.form.get('year', type=int)
    week = request.form.get('week', type=int)
    week_offset = request.form.get('week_offset', type=int)

    schedule = Schedule(store=store,
                        user_id=user_id,
                        user_name=user_name,
                        dep=dep,
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
def addNewStore():
    user_id = request.args.get('user_id')
    store = request.args.get('store')
    dep = request.args.get('dep')

    deps = []
    deps.append(dep)

    store = Store(store=store,
                  user_id=user_id,
                  deps=deps)

    store.put()

    # Setup results
    code = 0
    desc = 'Upload Successful'

    return jsonify(code=code, desc=desc)


# GETS  ######################################################################
@app.route('/api/stores/all')
def getStores():
    """Return all of the users stores"""

    user_id = request.args.get('user_id')

    stores = Store.query(Store.user_id == user_id).fetch()

    # convert to dicts
    stores = [s.to_dict() for s in stores]

    return jsonify(stores=stores)


@app.route('/api/upload/link')
def uploadImageLink():
    """Return a blobstore upload link as json for the client to upload an
    image.
    """
    upload_url = blobstore.create_upload_url('/api/upload_image')

    return jsonify(upload_url=upload_url)


@app.route('/api/users/<user_id>')
def getSchedulesByUser(user_id):
    """Return the users info    """

    return jsonify(user_id=user_id)


@app.route('/api/schedules/all')
def getSchedules():
    """Return all of the users schedules"""

    user_id = request.args.get('user_id')
    reverse = request.args.get('reverse')

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
