async def execute_action(action, color):
    if action == 'goAhead':
        await follow_line(Direction.STRAIGHT)
    elif action == 'turnLeft':
        await rotate(90, 90)
    elif action == 'turnRight':
        await rotate(-90, 90)
    elif action == 'wait':
        await asyncio.sleep(1)
    elif action == 'atStart' and DEPOT_MODE:
        await set_light_color(*color)
    elif action == 'atGoal' and DEPOT_MODE:
        await set_front_center_color(*color)
    else:
        raise ValueError(f"Unknown action: {action}")

async def set_light_color(r, g, b):
    await set_led(
        LEDMask.TOP | LEDMask.FRONT_LEFT | LEDMask.FRONT_CENTER | LEDMask.FRONT_RIGHT,
        RawColor(r / 255, g / 255, b / 255),
    )

async def set_front_center_color(r, g, b):
    await set_led(
        LEDMask.TOP | LEDMask.FRONT_LEFT | LEDMask.FRONT_RIGHT,
        RawColor(0, 0, 0),
    )
    await set_led(
        LEDMask.FRONT_CENTER,
        RawColor(r / 255, g / 255, b / 255),
    )
