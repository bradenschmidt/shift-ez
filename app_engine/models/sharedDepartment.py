from google.appengine.ext import ndb
from department import Department


class SharedDepartment(ndb.Model):
    """A department which has been shared. A key is created and used to recover
    this department so another user can join it. shared_dateTime - the
    datetime the store was shared for use as the expiry of the key, key - a
    hex uuid set during the creation so it can be recovered by another user
    securely, dep_key - the key of the Store that has been shared.
    """
    shared_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    key = ndb.StringProperty(required=True)
    dep_key = ndb.KeyProperty(Department, required=True)
