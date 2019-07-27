package com.noble.activity.seasons.spring

class RainDrop(var x: Int, var y: Int, var speed: Int, var alpha: Float) {

    init {
        if (speed <= 0) {
            speed = 1
        }

        if (alpha < 0.2f) {
            alpha = 0.2f
        } else if (alpha > 1) {
            alpha = 1f
        }
    }
}