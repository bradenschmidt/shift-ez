import os
import logging

import jinja2

from flask import Flask, url_for, jsonify, request

from operator import itemgetter

from google.appengine.api import images  # , users
from google.appengine.ext import ndb, blobstore

from werkzeug.http import parse_options_header

from models.schedule import Schedule


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


def schedule_key(schedule_name=DEFAULT_SCHEDULE_NAME):
    """Constructs a Datastore key for a Schedule entity.

    We use schedule_name as the key.
    """
    return ndb.Key('Schedule', schedule_name)


def createSchedules():
    schedule1 = Schedule(store='8th St Coop Home Centre',
                         user_id='bps',
                         user_name='Braden',
                         dep='Lumber',
                         year=2015,
                         week=18,
                         image=url_for('static',
                                       filename='images/schedule/s1.jpg'))

    schedule2 = Schedule(store='8th St Coop Home Centre',
                         user_id='zap',
                         user_name='Zach',
                         dep='Lumber',
                         year=2015,
                         week=19,
                         image=url_for('static',
                                       filename='images/schedule/s2.jpg'))

    schedule1.put()
    schedule2.put()


def getSchedules():
    # Fetch schedules by user_id and convert to dicts
    schedules = [s.to_dict() for s in
                 Schedule.query(Schedule.user_id == 'bps').fetch()]

    # print(schedules)

    if(len(schedules) > 0):
        # Sort schedules by year then week
        schedules = sorted(schedules, key=itemgetter('week'), reverse=True)
        schedules = sorted(schedules, key=itemgetter('year'), reverse=True)

    return schedules


def getSchedulesByKey(user_id='', store='', dep='', year='', week=''):
    if(user_id):
        schedules = Schedule.query(Schedule.user_id == user_id).fetch()
    elif(store):
        print store
        if(dep):
            print dep
            schedules = Schedule.query(ndb.AND(Schedule.store == store,
                                       Schedule.dep == dep)).fetch()
        else:
            schedules = Schedule.query(Schedule.store == store).fetch()
    elif(year):
        if(week):
            schedules = Schedule.query(ndb.AND(Schedule.week == week,
                                       Schedule.year == year)).fetch()
        else:
            schedules = Schedule.query(Schedule.year == year).fetch()

    else:
        logging.info('No keys given')
        schedules = []

    # Fetch schedules by user_id and convert to dicts
    schedules = [s.to_dict() for s in schedules]

    # print(schedules)

    return schedules


@app.route('/upload/form')
def uploadImageForm():
    upload_url = blobstore.create_upload_url('/upload_image')

    template_values = {
        'upload_url': upload_url
    }

    template = JINJA_ENVIRONMENT.get_template('upload.html')
    return template.render(template_values)


@app.route('/upload/link')
def uploadImageLink():
    """Return a blobstore upload link as json for the client to upload an
    image.
    """
    upload_url = blobstore.create_upload_url('/upload_image')

    return jsonify(upload_url=upload_url)


@app.route('/upload_image', methods=['POST'])
def uploadImage():
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

    schedule = Schedule(store=store,
                        user_id=user_id,
                        user_name=user_name,
                        dep=dep,
                        year=year,
                        week=week,
                        image_blob=blobstore.BlobKey(blob_key_str))

    schedule.put()

    return 'SUCCESS'


@app.route('/get')
def get():
    user_id = request.args.get('user_id')
    store = request.values.get('store')
    dep = request.args.get('dep')
    year = request.args.get('year')
    week = request.args.get('week')
    reverse = request.args.get('reverse')

    if(user_id):
        schedules = getSchedulesByKey(user_id=user_id)
    elif(store):
        if(dep):
            schedules = getSchedulesByKey(store=store, dep=dep)
        else:
            schedules = getSchedulesByKey(store=store)
    elif(year):
        if(week):
            schedules = getSchedulesByKey(year=int(year), week=int(week))
        else:
            schedules = getSchedulesByKey(year=int(year))
    else:
        logging.info('No args given')
        schedules = []

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


@app.route('/')
def index():
    # user = users.get_current_user()
    # if user:
    #     url = users.create_logout_url('')
    #     url_linktext = 'Logout'
    # else:
    #     url = users.create_login_url('')
    #     url_linktext = 'Login'

    schedules = getSchedules()

    for s in schedules:
        s['image'] = images.get_serving_url(s['image_blob'])
        del s['image_blob']

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
