from google.appengine.ext import ndb
from storeDepartment import StoreDepartment


class SharedStoreDepartment(ndb.Model):
    """A StoreDepartment which has been shared. A key is created and used to
    recover this department so another user can join it.
    shared_dateTime - the datetime the store was shared for use as the expiry
    of the key
    key - a hex uuid set during the creation so it can be recovered by another
    user securely
    dep_key - the key of the Store that has been shared.
    """
    shared_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    share_key = ndb.StringProperty(required=True)
    store_dep_key = ndb.KeyProperty(StoreDepartment, required=True)

    @staticmethod
    def get(_share_key):
        sharedStore = SharedStoreDepartment.query(
                        SharedStoreDepartment.share_key == _share_key).get()
        return sharedStore
