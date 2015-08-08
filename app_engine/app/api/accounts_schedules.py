from operator import itemgetter

from flask import jsonify, request, Blueprint
from google.appengine.ext.blobstore import blobstore
from werkzeug.http import parse_options_header

from app.models.account import Account
from app.models.schedule import Schedule
from app.models.storeDepartment import StoreDepartment
from app.utils.errors import Errors

accounts_schedules = Blueprint("accounts_schedules", __name__)


def sort_schedules(schedules, reverse):
    # sort
    if len(schedules) > 0:
        if reverse == 'true':
            # Sort schedules by year then week with newest schedule first
            schedules = sorted(schedules, key=itemgetter('week'), reverse=True)
            schedules = sorted(schedules, key=itemgetter('year'), reverse=True)
        else:
            # Sort schedules by year then week with oldest schedule first
            schedules = sorted(schedules, key=itemgetter('week'))
            schedules = sorted(schedules, key=itemgetter('year'))

    return schedules


# POSTS ##########################
@accounts_schedules.route('/add', methods=['POST'])
def upload_image(user_id):
    """ Accept an schedule with info and image"""
    image = request.files['file']

    header = image.headers['Content-Type']
    parsed_header = parse_options_header(header)
    blob_key_str = parsed_header[1]['blob-key']

    store_user_id = request.form.get('store_user_id')
    store_name = request.form.get('store_name')
    dep_name = request.form.get('dep_name')

    upload_user_id = request.form.get('upload_user_id')
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

    schedule = store.has_schedule(upload_user_id, year, week, week_offset)
    if schedule:
        # Update schedule
        schedule.image_blob = blobstore.BlobKey(blob_key_str)
        schedule.upload_user_id = user_id
        schedule.user_name = account.user_name

        schedule.put()

        # Setup results
        code = 0
        desc = 'Upload Successful. Store Updated'
    else:
        # No schedule found so make new one and add to store
        schedule = Schedule(parent=store.key,
                            upload_user_id=user_id,
                            user_name=account.user_name,
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


# GETS #############################################################
@accounts_schedules.route('/link')
def upload_image_link(user_id):
    """Return a blobstore upload link as json for the client to upload an
    image.
    """
    upload_url = blobstore.create_upload_url('/api/accounts/' + user_id + '/schedules/add')

    return jsonify(upload_url=upload_url)


@accounts_schedules.route('/all')
def get_schedules(user_id):
    """Return all of the users schedules sorted in reverse if specified."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    schedules = []
    if account:
        schedules = account.get_schedule_dicts()

    return jsonify(schedules=sort_schedules(schedules, reverse))


@accounts_schedules.route('/year/<year>')
def get_schedules_by_year(user_id, year):
    """Return the users schedules for a given year."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    year_schedules = []
    if account:
        schedules = account.get_schedules()
        for schedule in schedules:
            if schedule.year == int(year):
                year_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sort_schedules(year_schedules, reverse))


@accounts_schedules.route('/year/<year>/week/<week>')
def get_schedules_by_year_week(user_id, year, week):
    """Return the users schedules for a given year and week."""
    reverse = request.args.get('reverse')

    account = Account.get(user_id)
    week_schedules = []
    if account:
        schedules = account.get_schedules()
        for schedule in schedules:
            if schedule.year == int(year) and schedule.week == int(week):
                week_schedules.append(schedule.to_dict_images())

    return jsonify(schedules=sort_schedules(week_schedules, reverse))
