package com.a.retrofitsample

data class RepoBean(
    var default_branch: String?,
    var full_name: String?,
    var id: Int?,
    var name: String?,
    var node_id: String?,
    var open_issues: Int?,
    var `private`: Boolean?,
    var watchers: Int?
)