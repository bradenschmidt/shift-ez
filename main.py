import os

from flask import Flask, url_for, jsonify, request

import jinja2

# from google.appengine.api import users
from google.appengine.ext import ndb

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


def getSchedules():
    schedules = []

    schedule1 = Schedule(store='8th St Coop Home Centre',
                         user_id='bps',
                         user_name='Braden',
                         dep='Lumber',
                         week='1',
                         image=url_for('static', filename='images/schedule/s1.jpg'))

    schedule2 = Schedule(store='8th St Coop Home Centre',
                         user_id='zap',
                         user_name='Zach',
                         dep='Lumber',
                         week='2',
                         image=url_for('static', filename='images/schedule/s2.jpg'))

    schedules.append(schedule1.to_dict())
    schedules.append(schedule2.to_dict())

    return schedules


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
