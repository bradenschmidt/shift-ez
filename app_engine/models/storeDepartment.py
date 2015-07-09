from google.appengine.ext import ndb
import logging
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

    @staticmethod
    def get(_user_id, _store_name, _dep_name):
        store = StoreDepartment.query(
                        ndb.AND(StoreDepartment.store_name == _store_name,
                                StoreDepartment.dep_name == _dep_name,
                                StoreDepartment.user_id == _user_id)).get()
        return store

    def getSchedules(self):
        schedules = []
        for schedule_key in self.schedules:
            schedule = schedule_key.get()
            if schedule:
                self.schedules.append(schedule)
            else:
                logging.info("Schedule with given key is missing: "
                             + str(schedule_key.flat())
                             + " from StoreDepartment: " + self.user_id
                             + " - " + self.store_name
                             + " - " + self.dep_name)
        return schedules
