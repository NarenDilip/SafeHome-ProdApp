package com.salzerproduct.safehome.dummy

import com.salzerproduct.safehome.model.Device
import com.salzerproduct.safehome.model.Entity
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<Device> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Device> = HashMap()

    private val COUNT = 5

    init {
        // Add some sample items.
        for (i in 0..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: Device) {
        ITEMS.add(item)
        ITEM_MAP[item.id!!.id!!] = item
    }

    private fun createDummyItem(position: Int): Device {
        val device = Device()
        device.name = "Device $position"
        device.id = Entity()
        device.id!!.id = "${position + 1}"
        when (position) {
            in 0..2 -> device.type = "Door Sensor"
            3 -> device.type = "Remote"
            in 4..5 -> device.type = "Water Meter"
        }
        return device
    }

}
