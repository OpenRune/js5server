package dev.advo.js5

class Stopwatch {

    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var running: Boolean = false

    fun start() {
        if (!running) {
            startTime = System.nanoTime()
            running = true
        }
    }

    fun stop() {
        if (running) {
            elapsedTime += System.nanoTime() - startTime
            running = false
        }
    }

    fun reset() {
        startTime = 0
        elapsedTime = 0
        running = false
    }

    fun elapsedNanos(): Long {
        return if (running) elapsedTime + (System.nanoTime() - startTime) else elapsedTime
    }

    fun elapsedMillis(): Long {
        return elapsedNanos() / 1_000_000
    }

    fun isRunning(): Boolean = running
}
