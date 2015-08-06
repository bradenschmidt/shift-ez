from wtforms import Form, StringField, validators


class AddStoreForm(Form):
    store_name = StringField('Store Name', validators=[validators.DataRequired()])
    dep_name = StringField('Department Name')
