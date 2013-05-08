/*
 * Copyright 2013 Maurício Linhares
 *
 * Maurício Linhares licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.github.mauricio.async.db.mysql

import com.github.mauricio.async.db.exceptions.EncoderNotAvailableException
import com.github.mauricio.async.db.mysql.encoder.{QuitMessageEncoder, HandshakeResponseEncoder}
import com.github.mauricio.async.db.mysql.message.client.ClientMessage
import com.github.mauricio.async.db.mysql.util.CharsetMapper
import com.github.mauricio.async.db.util.{ChannelUtils, Log}
import java.nio.charset.Charset
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.channel.{Channel, ChannelHandlerContext}
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder
import scala.annotation.switch

object MySQLOneToOneEncoder {
  val log = Log.get[MySQLOneToOneEncoder]
}

class MySQLOneToOneEncoder( charset : Charset, charsetMapper : CharsetMapper ) extends OneToOneEncoder {

  private val handshakeResponseEncoder = new HandshakeResponseEncoder(charset, charsetMapper)
  private var sequence = 1

  def encode(ctx: ChannelHandlerContext, channel: Channel, msg: Any): ChannelBuffer = {

    msg match {
      case message : ClientMessage => {
        val encoder = (message.kind : @switch) match {
          case ClientMessage.ClientProtocolVersion => this.handshakeResponseEncoder
          case ClientMessage.Quit => {
            sequence = 0
            QuitMessageEncoder
          }
          case _ => throw new EncoderNotAvailableException(message)
        }

        val result = encoder.encode(message)

        ChannelUtils.writePacketLength(result, sequence)

        sequence += 1

        result
      }
    }

  }

}
