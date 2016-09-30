package org.bigbluebutton.connections

class ConnectionStateModel {
  private var connectionTime: Long = 0L
  private var disconnectionTime: Long = 0L

  def getConnectionTime = connectionTime
  def getDisconnectionTime = disconnectionTime
  def setConnectionTime(updatedValue: Long): Unit = {connectionTime = updatedValue}
  def setDisconnectionTime(updatedValue: Long): Unit = {disconnectionTime = updatedValue}
}
