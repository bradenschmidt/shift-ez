from google.appengine.ext import ndb
from store import Store


class Account(ndb.Model):
    """user_id - id of user (email), user_name - Name of user, stores - list
    of Stores the user has.
    """
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    stores = ndb.KeyProperty(Store, repeated=True)
