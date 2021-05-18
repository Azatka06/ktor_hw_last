package model

class Location(val lat: Double, val lng: Double)

infix fun Double.x(that: Double) = Location(this, that)
