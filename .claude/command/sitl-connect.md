# SITL Connect

PX4 SITLを起動してアプリから接続する。

## 手順
1. PX4 SITL起動
```bash
cd ~/PX4-Autopilot
make px4_sitl gazebo-classic
```

2. 接続確認
- エミュレーター: `10.0.2.2:14550`
- 実機: `[PCのIPアドレス]:14550`

3. HEARTBEATが届いたら接続成功

## トラブルシューティング
- ポート確認: `lsof -i :14550`
- Firewall確認: SITLのUDP 14550を許可