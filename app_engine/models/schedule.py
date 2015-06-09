from google.appengine.ext import ndb


class Schedule(ndb.Model):
    upload_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    store = ndb.StringProperty(required=True)
    dep = ndb.StringProperty(required=True)
    year = ndb.IntegerProperty(required=True)
    week = ndb.IntegerProperty(required=True)
    image_blob = ndb.BlobKeyProperty()
