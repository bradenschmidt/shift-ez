from google.appengine.ext import ndb
from schedule import Schedule


class StoreDepartment(ndb.Model):
    """
    store_name - Name of the store, deps - list of Departments the store
    has.
    dep_name - Name of this department, schedules - List of schedules for this
    department.
    schedules - A list of Schedule Keys this store has
    """
    user_id = ndb.StringProperty(required=True)

    store_name = ndb.StringProperty(required=True)
    dep_name = ndb.StringProperty(required=True)

    schedules = ndb.KeyProperty(Schedule, repeated=True)
