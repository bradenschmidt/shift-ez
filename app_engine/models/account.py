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
        account = Account.query(Account.user_id == _user_id).get()
        return account

    def getStoreDeps(self):
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

    def doesStoreExistInAccount(self, _user_id, _store_name, _dep_name):
        stores = self.getStoreDeps()
        for store in stores:
            if store.user_id == _user_id \
                    and store.store_name == _store_name \
                    and store.dep_name == _dep_name:
                return True

        return False

    def getStoreFromAccount(self, _user_id, _store_name, _dep_name):
        stores = self.getStoreDeps()
        for store in stores:
            if store.user_id == _user_id \
                    and store.store_name == _store_name \
                    and store.dep_name == _dep_name:
                return store

        return None

    def to_dict_stores(self):
        accountDict = self.to_dict()

        stores = []
        for store in self.getStoreDeps():
            storeDict = store.to_dict()
            stores.append(storeDict)
        accountDict['storeDeps'] = stores

        return accountDict
