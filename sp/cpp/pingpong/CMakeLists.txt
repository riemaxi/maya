cmake_minimum_required(VERSION 3.10)
project(pingpong)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 1. Find Threads FIRST (ixwebsocket needs this)
find_package(Threads REQUIRED)

# 2. Find the other libraries
find_package(nlohmann_json REQUIRED)
find_package(OpenSSL REQUIRED)
find_package(ixwebsocket REQUIRED)

# 3. Include current directory
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

# 4. Create the executable
add_executable(pingpong main.cpp)

# 5. Link everything
target_link_libraries(pingpong PRIVATE 
    ixwebsocket::ixwebsocket 
    nlohmann_json::nlohmann_json
    OpenSSL::SSL 
    OpenSSL::Crypto
    Threads::Threads
)