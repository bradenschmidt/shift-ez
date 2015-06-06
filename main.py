import os
import urllib

from flask import Flask, url_for

import jinja2
import webapp2

from google.appengine.api import users
from google.appengine.ext import ndb

app = Flask(__name__)
app.config['DEBUG'] = True


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)


@app.route('/')
def homepage():
    user = users.get_current_user()
    if user:
        url = users.create_logout_url('')
        url_linktext = 'Logout'
    else:
        url = users.create_login_url('')
        url_linktext = 'Login'

    schedules  = []

    schedule = {
        'user': 'Braden',
        'dep': 'Lumber',
        'week': '1',
        'image': url_for('static', filename='images/schedule/s1.jpg')
    }

    schedules.append(schedule)
    schedules.append(schedule)

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
