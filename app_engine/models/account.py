from google.appengine.ext import ndb
from storeDepartment import StoreDepartment


class Account(ndb.Model):
    """
    user_id - id of user (email)
    user_name - Name of user
    stores - list of StoreDepartment keys the user has.
    """
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    storeDeps = ndb.KeyProperty(StoreDepartment, repeated=True)
