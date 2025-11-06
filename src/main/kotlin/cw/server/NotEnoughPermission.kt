package cw.server

class NotEnoughPermission(action: String) :
    Exception("You do not have enough permission to $action.")
