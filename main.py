import os
import logging

from flask import Flask, url_for, jsonify, request

from operator import itemgetter

import jinja2

from google.appengine.api import images  # , users
from google.appengine.ext import ndb, blobstore

from werkzeug.http import parse_options_header

from models.schedule import Schedule

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
        schedules = sorted(schedules, key=itemgetter('year', 'week'))

    return schedules


@app.route('/upload')
def uploadImageForm():
    upload_url = blobstore.create_upload_url('/upload_image')

    template_values = {
        'upload_url': upload_url
    }

    template = JINJA_ENVIRONMENT.get_template('upload.html')
    return template.render(template_values)


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


def getSchedulesByUsername(username):
    schedules = getSchedules()

    s = []

    for schedule in schedules:
        if(schedule.username == username):
            s.append(schedule)

    return s


@app.route('/get')
def get():
    username = request.args.get('username')

    if(username):
        schedules = getSchedulesByUsername()
    else:
        schedules = getSchedules()

    print(schedules)

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
