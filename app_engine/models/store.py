from google.appengine.ext import ndb
from department import Department


class Store(ndb.Model):
    """store_name - Name of the store, deps - list of Departments the store
    has.
    """
    user_id = ndb.StringProperty(required=True)
    store_name = ndb.StringProperty(required=True)
    deps = ndb.KeyProperty(Department, repeated=True)
