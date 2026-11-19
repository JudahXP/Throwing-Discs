from pathlib import Path
repo=Path(__file__).resolve().parents[2]
for v in ['1.20.2','1.20.3','1.20.4','1.20.5','1.20.6']:
 r=repo/'versions'/('judahs-throwing-discs-'+v)
 p=r/'src/main/java/dev/disc/DiscDispense.java';s=p.read_text().replace('net.minecraft.core.BlockSource','net.minecraft.core.dispenser.BlockSource').replace('source.<net.minecraft.world.level.block.entity.DispenserBlockEntity>getEntity()','source.blockEntity()').replace('source.getLevel()','source.level()').replace('source.getPos()','source.pos()').replace('source.getBlockState()','source.state()');p.write_text(s)
 p=r/'src/main/java/dev/disc/ThrowingDiscEntity.java';p.write_text(p.read_text().replace('int steps,boolean teleport','int steps'))
 p=r/'src/gametest/java/dev/disc/DiscGameTests.java';s=p.read_text().replace('"disc-test"));','"disc-test"),net.minecraft.server.level.ClientInformation.createDefault());').replace('placeNewPlayer(c,p);','placeNewPlayer(c,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile()'+(',false'if v in ['1.20.5','1.20.6']else'')+'));').replace('recipe.assemble(', 'recipe.value().assemble(')
 if v in ['1.20.2','1.20.3','1.20.4']:s=s.replace('new io.netty.channel.embedded.EmbeddedChannel(c);','var channel=new io.netty.channel.embedded.EmbeddedChannel(c);channel.attr(net.minecraft.network.Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(net.minecraft.network.ConnectionProtocol.PLAY.codec(net.minecraft.network.protocol.PacketFlow.SERVERBOUND));channel.attr(net.minecraft.network.Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(net.minecraft.network.ConnectionProtocol.PLAY.codec(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND));')
 p.write_text(s)
