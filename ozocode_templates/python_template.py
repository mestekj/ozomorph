import ozobot
import asyncio
from _ozo import DeviceManager
from _ozo import get_robot
import math

# To be inserted
agents = ...
# agents = {0: ((255, 0, 0), ['turnLeft', 'goAhead', 'goAhead']), 1: ((255, 0, 0), ['wait', 'turnLeft', 'goAhead']), 2: ((0, 0, 255), ['wait', 'turnLeft', 'goAhead'])}


dm = DeviceManager()

for agent in agents:
    dm.add_device(f"Evo_{agent}", get_robot(f"Evo_{agent}", "Evo"))

async def execute_action(action):
    if action == 'goAhead':
        await dm.navigation.anavigate(ozobot.Directions.FORWARD, follow = True)
    elif action == 'turnLeft':
        await dm.movement.arotate(90 * (math.pi / 180), 90 * (math.pi / 180))
    elif action == 'turnRight':
        await dm.movement.arotate(- 90 * (math.pi / 180), 90 * (math.pi / 180))
    elif action == 'wait':
        await asyncio.sleep(1)

async def set_light_color(r, g, b):
    await dm.light_effects.aset_light_color_rgb(r / 255, g / 255, b / 255, ozobot.Lights.TOP | ozobot.Lights.FRONT_1 | ozobot.Lights.FRONT_3 | ozobot.Lights.FRONT_5)

async def main():
    async with asyncio.TaskGroup() as _tg:
        async def _coroutine_folow_plan(agent):
            device_name = f"Evo_{agent}"
            plan = agents[agent][1]
            r,g,b = agents[agent][0]

            with dm.use_device(device_name):
                await set_light_color(r, g, b)
                await dm.navigation.aset_line_following_speed(50 / 1000.0)
                for action in plan:
                    await execute_action(action)

        tasks = tuple(_tg.create_task(_coroutine_folow_plan(agent)) for agent in agents)
        await asyncio.gather(*tasks)


asyncio.run(main())
