package com.theminesec.example.headless

import com.theminesec.example.headless.landing.LandingMain
import com.theminesec.sdk.headless.HeadlessActivity

class Main : LandingMain() {
    override val headlessImplClass: Class<out HeadlessActivity>
        get() = ExampleBdoHeadlessImpl::class.java
}
