from google.appengine.ext import ndb


class SharedStore(ndb.Model):
    upload_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    store_name = ndb.StringProperty(required=True)
    dep = ndb.StringProperty(required=True)
    key = ndb.StringProperty(required=True)
