import com.intellij.jvm.dfa.analysis.dev.config.TaintConfig
import com.intellij.jvm.dfa.analysis.configurator.taint.rules.TaintRule

TaintConfig {
    method("java.util.stream.Stream.filter") { _ -> { } }
}