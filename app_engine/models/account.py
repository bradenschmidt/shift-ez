from google.appengine.ext import ndb
import logging
from storeDepartment import StoreDepartment


class Account(ndb.Model):
    """
    user_id - id of user (email)
    user_name - Name of user
    stores - list of StoreDepartment keys the user has.
    """
    user_id = ndb.StringProperty(required=True)
    user_name = ndb.StringProperty(required=True)
    storeDeps = ndb.KeyProperty(StoreDepartment, repeated=True)

    @staticmethod
    def get(_user_id):
        """Return the account with the given user_id"""
        account = Account.query(Account.user_id == _user_id).get()
        return account

    def getStoreDeps(self):
        """Get the stores for this account."""
        stores = []
        for store_key in self.storeDeps:
            store = store_key.get()
            if store:
                stores.append(store)
            else:
                logging.info("Store with given key is missing: "
                             + str(store_key.flat()) + " for Account: "
                             + self.user_id)
        return stores

    def isStoreInAccount(self, store_to_find):
        """Check if a specific store is in this account."""
        stores = self.getStoreDeps()
        if store_to_find in stores:
            return True

        return False

    def getStoreFromAccount(self, _user_id, _store_name, _dep_name):
        """Get a specific store by the store user id, store name, and dep name
        from this account.
        """
        stores = self.getStoreDeps()
        for store in stores:
            if store.user_id == _user_id \
                    and store.store_name == _store_name \
                    and store.dep_name == _dep_name:
                return store

        return None

    def getSchedules(self):
        """Get all the schedule objects for all the stores the user has
        one.
        """
        stores = self.getStoreDeps()

        schedules = []
        if stores:
            for store in stores:
                store_schedules = store.getSchedules()
                for schedule in store_schedules:
                    schedules.append(schedule)

        return schedules

    def getScheduleDicts(self):
        """Get all the users schedules from all the stores as dicts."""
        stores = self.getStoreDeps()

        schedules = []
        if stores:
            for store in stores:
                store_schedules = store.getScheduleDicts()
                for schedule in store_schedules:
                    schedules.append(schedule)

        return schedules

    def to_dict_stores(self):
        """Account to_dict with stores included."""
        accountDict = self.to_dict()

        stores = []
        for store in self.getStoreDeps():
            stores.append(store.to_dict_schedules())
        accountDict['storeDeps'] = stores

        return accountDict
