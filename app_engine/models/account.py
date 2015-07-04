from google.appengine.ext import ndb
from store import Store


class Account(ndb.Model):
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    stores = ndb.KeyProperty(Store, repeated=True)
