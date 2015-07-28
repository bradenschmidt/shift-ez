from google.appengine.ext import ndb
from google.appengine.api import images


class Schedule(ndb.Model):
    """
    upload_dateTime - Datetime the upload took place (auto added on upload)
    year - the year of this schedule
    week - The week of the year this schedule is for
    week_offset - The Offset between the schedules year and the true week of
    the year
    image_blob - blob key for the image of this schedule.
    """
    parent = ndb.KeyProperty('StoreDepartment', required=True)

    upload_user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    upload_dateTime = ndb.DateTimeProperty(auto_now_add=True)
    year = ndb.IntegerProperty(required=True)
    week = ndb.IntegerProperty(required=True)
    week_offset = ndb.IntegerProperty(required=True)
    image_blob = ndb.BlobKeyProperty()

    @staticmethod
    def get(_upload_user_id, _year, _week, _week_offset):
        schedule = Schedule.query(
            ndb.AND(Schedule.year == _year,
                    Schedule.week == _week,
                    Schedule.week_offset == _week_offset,
                    Schedule.upload_user_id == _upload_user_id)).get()
        return schedule

    def to_dict_images(self):
        schedule_dict = self.to_dict()

        # Remove parent key
        del schedule_dict['parent']

        # Convert blob key to image url for each schedule, the remove blob key
        schedule_dict['image_url'] = images.get_serving_url(
            schedule_dict['image_blob'])
        del schedule_dict['image_blob']

        return schedule_dict
