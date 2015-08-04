import json
import os
from pprint import pprint

import jinja2

from flask import Flask

from google.appengine.ext import blobstore

from app.api.accounts import accounts
from app.api.accounts_schedules import accounts_schedules, sort_schedules
from app.api.accounts_stores import accounts_stores, get_accounts_stores, get_schedules_by_store
from app.api.api import my_api
from app.models.schedule import Schedule

"""
    Use 'dev_appserver.py .' to deploy locally for testing
    Use 'appcfg.py -A shift-ez update app' to deploy on gae
"""

app = Flask(__name__)
app.config['DEBUG'] = True


# Setup api
app.register_blueprint(my_api, url_prefix='/api')
app.register_blueprint(accounts, url_prefix='/api/accounts/<user_id>')
app.register_blueprint(accounts_stores, url_prefix='/api/accounts/<user_id>/stores')
app.register_blueprint(accounts_schedules, url_prefix='/api/accounts/<user_id>/schedules')


JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)


def get_all_schedules():
    schedules = Schedule.query().fetch()
    schedules = [s.to_dict_images() for s in schedules]
    return sort_schedules(schedules, None)


def get_json_response(view_name, *args, **kwargs):
    """Calls internal view method, parses json, and returns python dict."""
    view = app.view_functions[view_name]
    res = view(*args, **kwargs)
    js = json.loads(res.data)
    return js


# Pages ######################################################################
@app.route('/stores/<store_name>/dep/<dep_name>')
def store_template(store_name, dep_name):
    """Serve the homepage."""

    # user = users.get_current_user()
    # if user:
    #     url = users.create_logout_url('')
    #     url_linktext = 'Logout'
    # else:
    #     url = users.create_login_url('')
    #     url_linktext = 'Login'

    user_id = "bradenschmidt@gmail.com"
    resp = get_accounts_stores(user_id)
    stores = json.loads(resp.data)

    resp_store = get_schedules_by_store(user_id, store_name, dep_name, store_user_id=user_id)
    store = json.loads(resp_store.data)

    # pprint(stores['stores'])
    pprint(store)

    template_values = {
        'schedules': store['schedules'],
        'stores': stores['stores']
    }

    template = JINJA_ENVIRONMENT.get_template('app/templates/store.html')
    return template.render(template_values)


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

    user_id = "bradenschmidt@gmail.com"
    resp = get_accounts_stores(user_id)
    stores = json.loads(resp.data)

    pprint(stores)

    template_values = {
        'stores': stores['stores']
    }

    template = JINJA_ENVIRONMENT.get_template('app/templates/index.html')
    return template.render(template_values)


@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404


@app.route('/upload/form')
def upload_image_form():
    """Show an upload form"""

    user_id = ""

    upload_url = blobstore.create_upload_url('/api/accounts/' + user_id + '/schedules/add')

    template_values = {
        'upload_url': upload_url
    }

    template = JINJA_ENVIRONMENT.get_template('upload.html')
    return template.render(template_values)
