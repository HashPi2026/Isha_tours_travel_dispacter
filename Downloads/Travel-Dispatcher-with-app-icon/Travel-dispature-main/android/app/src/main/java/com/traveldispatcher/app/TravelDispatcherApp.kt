package com.traveldispatcher.app

import android.app.Application
import com.traveldispatcher.app.data.email.SmtpMailSender
import com.traveldispatcher.app.data.export.ExportManager
import com.traveldispatcher.app.data.network.NetworkMonitor
import com.traveldispatcher.app.data.repository.AgentRepository
import com.traveldispatcher.app.data.repository.InquiryRepository
import com.traveldispatcher.app.data.repository.LedgerRepository
import com.traveldispatcher.app.data.security.KeystoreManager

class TravelDispatcherApp : Application() {

    lateinit var keystoreManager: KeystoreManager
        private set

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var inquiryRepository: InquiryRepository
        private set

    lateinit var ledgerRepository: LedgerRepository
        private set

    lateinit var agentRepository: AgentRepository
        private set

    lateinit var smtpMailSender: SmtpMailSender
        private set

    lateinit var exportManager: ExportManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        keystoreManager = KeystoreManager(this)
        networkMonitor = NetworkMonitor(this)
        inquiryRepository = InquiryRepository()
        ledgerRepository = LedgerRepository()
        agentRepository = AgentRepository()
        smtpMailSender = SmtpMailSender(inquiryRepository)
        exportManager = ExportManager(this)
    }

    companion object {
        lateinit var instance: TravelDispatcherApp
            private set
    }
}
