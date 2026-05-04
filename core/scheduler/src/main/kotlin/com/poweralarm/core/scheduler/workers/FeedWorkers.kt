package com.poweralarm.core.scheduler.workers

/**
 * Marker identifiers for periodic feed-refresh workers. Concrete `CoroutineWorker`
 * implementations live in their respective integration modules; the scheduler module
 * only owns the unique-work names and frequency settings keys.
 */
object FeedWorkers {
    const val WEATHER = "feed.weather"
    const val TRAFFIC = "feed.traffic"
    const val AQI = "feed.aqi"
    const val HOLIDAYS = "feed.holidays"
    const val ICS = "feed.ics"
    const val SYNC = "feed.sync"
    const val TFL = "feed.tfl"
    const val EMERGENCY = "feed.emergency"
}
