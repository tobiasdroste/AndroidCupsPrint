package io.github.benoitduffez.cupsprint.app

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import io.github.benoitduffez.cupsprint.HttpConnectionManagement
import io.github.benoitduffez.cupsprint.R
import io.github.benoitduffez.cupsprint.databinding.UntrustedCertBinding
import java.security.cert.X509Certificate

/**
 * Show an untrusted cert info + two buttons to accept or refuse to trust said cert
 */
class UntrustedCertActivity : Activity() {

    private lateinit var binding: UntrustedCertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UntrustedCertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val cert = intent.getSerializableExtra(KEY_CERT) as X509Certificate

        // Build short cert description
        val sb = StringBuilder()
        sb.append("Issuer: ").append(cert.issuerX500Principal.toString())
        sb.append("\nValidity: not before ").append(cert.notBefore.toString())
        sb.append("\nValidity: not after ").append(cert.notAfter.toString())
        sb.append("\nSubject: ").append(cert.subjectX500Principal.name)
        sb.append("\nKey algo: ").append(cert.sigAlgName)
        binding.untrustedCertinfo.text = sb

        binding.untrustedTrustButton.setOnClickListener {
            if (HttpConnectionManagement.saveCertificates(this, arrayOf(cert))) {
                Toast.makeText(
                    this@UntrustedCertActivity,
                    R.string.untrusted_trusted,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@UntrustedCertActivity,
                    R.string.untrusted_couldnt_trust,
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }

        binding.untrustedAbortButton.setOnClickListener { finish() }
    }

    companion object {
        val KEY_CERT = "${UntrustedCertActivity::class.java.name}.Certs"
    }
}
