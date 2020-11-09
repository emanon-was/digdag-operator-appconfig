package io.digdag.plugin.aws.appconfig.get_configuration

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider

case class AwsCredentials(val provider: AwsCredentialsProvider)
