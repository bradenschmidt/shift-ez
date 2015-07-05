from google.appengine.ext import ndb


class Schedule(ndb.Model):
    """
    upload_dateTime - Datetime the upload took place (auto added on upload)
    year - the year of this schedule
    week - The week of the year this schedule is for
    week_offset - The Offset between the schedules year and the true week of
    the year
    image_blob - blob key for the image of this schedule.
    """
    upload_user_id = ndb.StringProperty(required=True)
    upload_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    year = ndb.IntegerProperty(required=True)
    week = ndb.IntegerProperty(required=True)
    week_offset = ndb.IntegerProperty(required=True)
    image_blob = ndb.BlobKeyProperty()
