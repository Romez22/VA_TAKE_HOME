import Foundation
import Shared

enum DomainErrorCopy {
    static func loginMessage(_ e: DomainError) -> String {
        switch onEnum(of: e) {
        case .validation:
            return "Please check your email and password and try again."
        case .unauthorized:
            return "Invalid credentials. Please check your email and password."
        case .rateLimited:
            return "Too many attempts. Please wait a moment and try again."
        case .network:
            return "No connection. Check your network and try again."
        case .timeout:
            return "That took too long. Please try again."
        case .notFound, .conflict, .classInPast, .server, .serialization, .unknown:
            return "Something went wrong. Please try again."
        }
    }
}
