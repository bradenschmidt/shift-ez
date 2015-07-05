from google.appengine.ext import ndb
from schedule import Schedule


class Department(ndb.Model):
    """dep_name - Name of this department, schedules - List of schedules for
    this department.
    """
    user_id = ndb.StringProperty(required=True)
    dep_name = ndb.StringProperty(required=True)
    schedules = ndb.KeyProperty(Schedule, repeated=True)
