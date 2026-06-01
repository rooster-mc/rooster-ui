package dev.rooster.ui.interfaces.constructors.shop

import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.items.InterfaceItem

class ShopInterface

class ShopItem {
    fun <T : Context> addShopAttributes(
        item: InterfaceItem<T>,
        costs: InterfaceInfo<T>.() -> Number
    ) {

        item.onClick {

        }
    }
}
