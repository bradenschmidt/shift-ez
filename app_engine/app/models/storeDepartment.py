import logging

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

    # TODO solve multiple object access problem
    schedules = ndb.KeyProperty(Schedule, repeated=True)

    @staticmethod
    def get(_user_id, _store_name, _dep_name):
        store = StoreDepartment.query(
            ndb.AND(StoreDepartment.store_name == _store_name,
                    StoreDepartment.dep_name == _dep_name,
                    StoreDepartment.user_id == _user_id)).get()
        return store

    def has_schedule(self, upload_user_id, year, week, week_offset):
        for schedule_key in self.schedules:
            schedule = schedule_key.get()
            if schedule:
                if schedule.upload_user_id == upload_user_id and schedule.year == year \
                        and schedule.week == week and schedule.week_offset == week_offset:
                    return schedule

        return None

    def get_schedules(self):
        schedules = []

        for schedule_key in self.schedules:
            schedule = schedule_key.get()
            if schedule:
                schedules.append(schedule)
            else:
                logging.info("Schedule with given key is missing: "
                             + str(schedule_key.flat())
                             + " from StoreDepartment: " + self.user_id
                             + " - " + self.store_name
                             + " - " + self.dep_name)
        return schedules

    def get_schedule_dicts(self):
        schedules = []
        for schedule_key in self.schedules:
            schedule = schedule_key.get()
            if schedule:
                schedule_dict = schedule.to_dict_images()
                schedule_dict['store_name'] = self.store_name
                schedule_dict['dep_name'] = self.dep_name
                schedule_dict['store_user_id'] = self.user_id
                schedules.append(schedule_dict)
            else:
                logging.info("Schedule with given key is missing: "
                             + str(schedule_key.flat())
                             + " from StoreDepartment: " + self.user_id
                             + " - " + self.store_name
                             + " - " + self.dep_name)

        return schedules

    def to_dict_schedules(self):
        store_dict = self.to_dict()

        schedules = self.get_schedule_dicts()

        store_dict['schedules'] = schedules

        return store_dict
