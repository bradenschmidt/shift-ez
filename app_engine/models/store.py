from google.appengine.ext import ndb


class Store(ndb.Model):
    user_id = ndb.StringProperty(required=True)
    store = ndb.StringProperty(required=True)
    deps = ndb.JsonProperty(required=True)
