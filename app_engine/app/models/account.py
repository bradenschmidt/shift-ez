import logging

from google.appengine.ext import ndb

from storeDepartment import StoreDepartment


class Account(ndb.Model):
    """
    user_id - id of user (email)
    user_name - Name of user
    stores - list of StoreDepartment keys the user has.
    """
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    user_image_url = ndb.StringProperty(required=True)
    # TODO solve multiple object access problem
    store_deps = ndb.KeyProperty(StoreDepartment, repeated=True)

    @staticmethod
    def get(_user_id):
        """Return the account with the given user_id"""
        account = Account.query(Account.user_id == _user_id).get()
        return account

    def get_store_deps(self):
        """Get the stores for this account.
        :rtype : List
        """
        stores = []
        for store_key in self.store_deps:
            store = store_key.get()
            if store:
                stores.append(store)
            else:
                logging.info("Store with given key is missing: "
                             + str(store_key.flat()) + " for Account: "
                             + self.user_id)
        return stores

    def is_store_in_account(self, store_to_find):
        """Check if a specific store is in this account.
        :rtype : Boolean
        """
        stores = self.get_store_deps()
        if store_to_find in stores:
            return True

        return False

    def get_store_from_account(self, _user_id, _store_name, _dep_name):
        """Get a specific store by the store user id, store name, and dep name
        from this account.
        """
        stores = self.get_store_deps()
        for store in stores:
            if store.user_id == _user_id \
                    and store.store_name == _store_name \
                    and store.dep_name == _dep_name:
                return store

        return None

    def get_schedules(self):
        """Get all the schedule objects for all the stores the user has
        one.
        """
        stores = self.get_store_deps()

        schedules = []
        if stores:
            for store in stores:
                store_schedules = store.get_schedules()
                for schedule in store_schedules:
                    schedules.append(schedule)

        return schedules

    def get_schedule_dicts(self):
        """Get all the users schedules from all the stores as dicts."""
        stores = self.get_store_deps()

        schedules = []
        if stores:
            for store in stores:
                store_schedules = store.get_schedule_dicts()
                for schedule in store_schedules:
                    schedules.append(schedule)

        return schedules

    def to_dict_stores(self):
        """Account to_dict with stores included."""
        account_dict = self.to_dict()

        stores = []
        for store in self.get_store_deps():
            stores.append(store.to_dict_schedules())
        account_dict['store_deps'] = stores

        return account_dict
