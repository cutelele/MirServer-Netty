package com.zhaoxiaodan.mirserver.loginserver;

import com.zhaoxiaodan.mirserver.gameserver.entities.Player;
import com.zhaoxiaodan.mirserver.gameserver.entities.User;
import com.zhaoxiaodan.mirserver.gameserver.types.Gender;
import com.zhaoxiaodan.mirserver.gameserver.types.Job;
import com.zhaoxiaodan.mirserver.network.Protocol;
import com.zhaoxiaodan.mirserver.network.packets.ClientPacket;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class LoginClientPackets {

	public static final class Process extends ClientPacket {

		public Process(byte cmdIndex) {
			super(Protocol.CM_PROTOCOL, cmdIndex);
		}
	}

	public static final class Login extends ClientPacket {

		public User user;

		public Login() {
		}

		public Login(byte cmdIndex, User user) {
			super(Protocol.CM_IDPASSWORD, cmdIndex);
			this.user = user;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);

			user = new User();
			String   conten = in.toString(Charset.defaultCharset()).trim();
			String[] parts  = conten.split(CONTENT_SEPARATOR_STR);
			if (parts.length > 1) {
				user.loginId = parts[0];
				user.password = parts[1];
			} else {
				throw new WrongFormatException();
			}
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);

			out.writeBytes(user.loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(user.password.getBytes());
		}
	}

	public static final class NewUser extends ClientPacket {

		public User user;

		public NewUser() {
		}

		public NewUser(byte cmdIndex, User user) {
			super(Protocol.CM_ADDNEWUSER, cmdIndex);
			this.user = user;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);

			user = new User();
			user.loginId = readString(in);
			user.password = readString(in);
			user.username = readString(in);
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(user.loginId.getBytes());
			out.writeBytes(new byte[]{0, 0, 0, 0});
			out.writeBytes(user.password.getBytes());
			out.writeBytes(new byte[]{0, 0, 0, 0});
			out.writeBytes(user.username.getBytes());
			out.writeBytes(new byte[]{0, 0, 0, 0});

		}

		private String readString(ByteBuf in) {
			StringBuilder sb = new StringBuilder();
			while (in.readableBytes() > 0) {
				byte c = in.readByte();
				if (c < 0x08) {
					if (0 == sb.length()) // 开头就是 空
						continue;
					else {
						break;
					}
				} else {
					sb.append((char) c);
				}
			}

			return sb.toString().trim();
		}

	}

	public static final class SelectServer extends ClientPacket {

		public String serverName;

		public SelectServer() {}

		public SelectServer(byte cmdIndex, String serverName) {
			super(Protocol.CM_SELECTSERVER, cmdIndex);
			this.serverName = serverName;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			serverName = in.toString(Charset.defaultCharset()).trim();
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(serverName.getBytes());
		}
	}

	public static final class NewCharacter extends ClientPacket {

		public Player player;

		public NewCharacter() {}

		public NewCharacter(byte cmdIndex, Player player) {
			super(Protocol.CM_NEWCHR, cmdIndex);
			this.player = player;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			String   content = in.toString(Charset.defaultCharset()).trim();
			String[] parts   = content.split(CONTENT_SEPARATOR_STR);
			if (parts.length < 5)
				throw new WrongFormatException();
			User user = new User();
			user.loginId = parts[0];
			player = new Player();
			player.user = user;
			player.name = parts[1];
			player.hair = Byte.parseByte(parts[2]);
			player.job = Job.values()[Byte.parseByte(parts[3])];
			player.gender = Gender.values()[Byte.parseByte(parts[4])];
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(player.user.loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(player.name.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(player.hair + '0');
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(player.job.ordinal() + '0');
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeByte(player.gender.ordinal() + '0');
			out.writeBytes(new byte[10]);
		}
	}

	public static final class QueryCharacter extends ClientPacket {

		public String loginId;
		public short  cert;

		public QueryCharacter() {}

		public QueryCharacter(byte cmdIndex, String loginId, short cert) {
			super(Protocol.CM_QUERYCHR, cmdIndex);
			this.loginId = loginId;
			this.cert = cert;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			String   content = in.toString(Charset.defaultCharset()).trim();
			String[] parts   = content.split(CONTENT_SEPARATOR_STR);
			if (parts.length >= 2) {
				loginId = parts[0];
				cert = Short.parseShort(parts[1]);
			} else {
				throw new WrongFormatException();
			}
		}


		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(Short.toString(cert).getBytes());
		}
	}

	public static final class DeleteCharacter extends ClientPacket {

		public String characterName;

		public DeleteCharacter() {}

		public DeleteCharacter(byte cmdIndex, String characterName) {
			super(Protocol.CM_QUERYCHR, cmdIndex);
			this.characterName = characterName;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			this.characterName = in.toString(Charset.defaultCharset()).trim();
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(this.characterName.getBytes());
		}
	}

	public static final class SelectCharacter extends ClientPacket {

		public String loginId;
		public String characterName;

		public SelectCharacter() {}

		public SelectCharacter(byte cmdIndex, String loginId, String characterName) {
			super(Protocol.CM_SELCHR, cmdIndex);
			this.loginId = loginId;
			this.characterName = characterName;
		}

		@Override
		public void readPacket(ByteBuf in) throws WrongFormatException {
			super.readPacket(in);
			String   content = in.toString(Charset.defaultCharset()).trim();
			String[] parts   = content.split(CONTENT_SEPARATOR_STR);
			if (parts.length >= 2) {
				loginId = parts[0];
				characterName = parts[1];
			} else {
				throw new WrongFormatException();
			}
		}

		@Override
		public void writePacket(ByteBuf out) {
			super.writePacket(out);
			out.writeBytes(this.loginId.getBytes());
			out.writeByte(CONTENT_SEPARATOR_CHAR);
			out.writeBytes(this.characterName.getBytes());
		}
	}
}
