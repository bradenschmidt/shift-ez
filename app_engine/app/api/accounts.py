from flask import request, jsonify, Blueprint

from app.models.account import Account

accounts = Blueprint("accounts", __name__, )


# POSTS ##########################
@accounts.route('/add', methods=['POST'])
def add_account():
    """Add a new Account"""
    user_id = request.args.get('user_id')
    user_name = request.args.get('user_name')
    user_image_url = request.args.get('user_image_url')

    account = Account.get(user_id)

    if account:
        # Setup results
        code = 1
        desc = 'Account Already Exists'
        account = account.to_dict_stores()
    else:
        new_account_model = Account(user_id=user_id,
                                    user_name=user_name,
                                    user_image_url=user_image_url,
                                    store_deps=[])
        new_account_model.put()
        account = new_account_model.to_dict_stores()

        # Setup results
        code = 0
        desc = 'Account Added Successfully'

    return jsonify(code=code, desc=desc, account=account)


# GETS #############################################################
@accounts.route('')
def get_account(user_id):
    """Return the Account Info"""

    account = Account.get(user_id)

    if account:
        account_dict = account.to_dict_stores()
    else:
        account_dict = None

    return jsonify(account=account_dict)
