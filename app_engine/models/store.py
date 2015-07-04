from google.appengine.ext import ndb


class Store(ndb.Model):
    store_name = ndb.StringProperty(required=True)
    deps = ndb.StringProperty(repeated=True)
