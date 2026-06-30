import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        do {
            KoinHelperKt.doInitKoin()
            print("✅ Koin initialized successfully")
        } catch {
            print("❌ Koin init failed: \(error)")
        }
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
