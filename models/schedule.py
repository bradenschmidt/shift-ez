from google.appengine.ext import ndb


class Schedule(ndb.Model):
    date = ndb.DateTimeProperty(auto_now_add=True)
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    store = ndb.StringProperty(required=True)
    dep = ndb.StringProperty(required=True)
    week = ndb.StringProperty(required=True)
    image = ndb.StringProperty(required=True)
