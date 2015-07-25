import re

from flask import Blueprint, jsonify, current_app

from app.models.storeDepartment import StoreDepartment

my_api = Blueprint('api', __name__)


# GETS  ######################################################################
@my_api.route('/stores/all')
def get_stores():
    """Return all of the stores"""

    stores = StoreDepartment.query().fetch()

    # convert to dicts
    stores = [s.to_dict() for s in stores]

    return jsonify(stores=stores)


@my_api.route('/help', methods=['GET'])
def api_help():
    """Print available functions."""
    func_list = {}
    for rule in current_app.url_map.iter_rules():
        if rule.endpoint != 'static':
            doc = current_app.view_functions[rule.endpoint].__doc__
            func_list[rule.rule] = re.sub(r"\s+", " ", doc)
    return jsonify(func_list)
